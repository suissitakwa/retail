import os
import requests
import gradio as gr
from dotenv import load_dotenv
from typing import Optional, Dict, Any, List

load_dotenv()
BACKEND_URL = os.getenv("RETAIL_BACKEND_URL", "http://localhost:8080")

# -----------------------------
# Helpers
# -----------------------------
def normalize_token(jwt_token: str) -> str:
    token = (jwt_token or "").strip()
    if token.lower().startswith("bearer "):
        token = token[7:].strip()
    return token

def backend_post_copilot(message: str, order_id: Optional[str], jwt_token: str) -> Dict[str, Any]:
    token = normalize_token(jwt_token)
    if not token:
        return {"error": "Please paste your JWT token."}

    payload: Dict[str, Any] = {"message": message}
    if order_id and order_id.strip():
        try:
            payload["orderId"] = int(order_id.strip())
        except ValueError:
            return {"error": "orderId must be a number (example: 752)."}

    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    r = requests.post(f"{BACKEND_URL}/api/v1/copilot/chat", json=payload, headers=headers, timeout=30)

    if r.status_code in (401, 403):
        return {"error": f"Auth failed ({r.status_code}). Token expired/invalid."}
    if not r.ok:
        return {"error": f"Backend error ({r.status_code}): {r.text}"}

    return r.json()

def backend_get_order_details(order_id: int, jwt_token: str) -> Dict[str, Any]:
    token = normalize_token(jwt_token)
    headers = {"Authorization": f"Bearer {token}"}
    r = requests.get(f"{BACKEND_URL}/api/v1/orders/{order_id}", headers=headers, timeout=30)

    if r.status_code in (401, 403):
        return {"error": f"Auth failed ({r.status_code})."}
    if not r.ok:
        return {"error": f"Backend error ({r.status_code}): {r.text}"}
    return r.json()

def backend_cancel_order(order_id: int, jwt_token: str) -> Dict[str, Any]:
    token = normalize_token(jwt_token)
    headers = {"Authorization": f"Bearer {token}"}
    r = requests.post(f"{BACKEND_URL}/api/v1/orders/{order_id}/cancel", headers=headers, timeout=30)

    if r.status_code in (401, 403):
        return {"error": f"Auth failed ({r.status_code})."}
    if not r.ok:
        return {"error": f"Cancel failed ({r.status_code}): {r.text}"}

    return {"ok": True}

def render_actions(actions: List[dict]) -> str:
    if not actions:
        return "_No actions_"
    lines = []
    for a in actions:
        name = a.get("name", "ACTION")
        summary = a.get("outputSummary", "")
        lines.append(f"- **{name}** — {summary}")
    return "\n".join(lines)

def pretty_json(data: Dict[str, Any]) -> str:
    return "```json\n" + str(data).replace("'", '"') + "\n```"


# -----------------------------
# Gradio handlers
# -----------------------------
def ask_copilot(message: str, order_id: str, jwt_token: str):
    resp = backend_post_copilot(message, order_id, jwt_token)

    if isinstance(resp, dict) and resp.get("error"):
        return resp["error"], "_No actions_", None, gr.update(visible=False), None, gr.update(visible=False), ""

    answer = resp.get("answer", "")
    actions = resp.get("actions", [])
    actions_md = render_actions(actions)

    order_id_for_details = None
    show_details_btn = False

    cancel_order_id = None
    show_cancel_btn = False

    for a in actions:
        name = a.get("name")
        inp = a.get("input") or {}
        oid = inp.get("orderId")

        if name == "OPEN_ORDER_DETAILS" and isinstance(oid, int):
            order_id_for_details = oid
            show_details_btn = True

        if name == "CANCEL_ORDER_CONFIRM" and isinstance(oid, int):
            cancel_order_id = oid
            show_cancel_btn = True

    return (
        answer,
        actions_md,
        order_id_for_details,
        gr.update(visible=show_details_btn),
        cancel_order_id,
        gr.update(visible=show_cancel_btn),
        ""  # clear confirm text
    )

def open_order_details(order_id_for_action, jwt_token: str):
    if not order_id_for_action:
        return "No orderId found."
    data = backend_get_order_details(int(order_id_for_action), jwt_token)
    if data.get("error"):
        return data["error"]
    return pretty_json(data)

def request_cancel(cancel_order_id, jwt_token: str):
    if not cancel_order_id:
        return "No orderId to cancel.", gr.update(visible=False)
    return f"⚠️ Confirm cancel orderId={cancel_order_id}? Type **YES** below and press Confirm.", gr.update(visible=True)

def confirm_cancel(cancel_order_id, confirm_text: str, jwt_token: str):
    if not cancel_order_id:
        return "No orderId to cancel.", "", gr.update(visible=False)

    if (confirm_text or "").strip().upper() != "YES":
        return "Please type YES to confirm.", "", gr.update(visible=True)

    result = backend_cancel_order(int(cancel_order_id), jwt_token)
    if result.get("error"):
        return result["error"], "", gr.update(visible=False)

    # after cancel, fetch updated order details
    data = backend_get_order_details(int(cancel_order_id), jwt_token)
    if data.get("error"):
        return "✅ Cancelled. (Could not refresh details)", "", gr.update(visible=False)

    return "✅ Order cancelled successfully.", pretty_json(data), gr.update(visible=False)


# -----------------------------
# UI
# -----------------------------
with gr.Blocks(title="Retail Support Copilot (Gradio)") as app:
    gr.Markdown("## 🛍️ Retail Support Copilot (Gradio Demo)\nTalk to your backend via `/api/v1/copilot/chat`.")

    jwt_input = gr.Textbox(label="JWT Token", placeholder="Paste JWT (with or without 'Bearer ')", type="password")
    message_input = gr.Textbox(label="Message", placeholder="Where is my order?", lines=2)
    order_id_input = gr.Textbox(label="Optional orderId", placeholder="752", max_lines=1)

    ask_btn = gr.Button("Ask Copilot")

    answer_out = gr.Markdown(label="Answer")
    actions_out = gr.Markdown(label="Actions")

    # For order details action
    order_id_state = gr.State(None)
    order_details_btn = gr.Button("Open Order Details", visible=False)
    order_details_out = gr.Markdown(label="Order Details")

    # For cancel flow
    cancel_order_id_state = gr.State(None)
    cancel_btn = gr.Button("Cancel Order", visible=False)

    cancel_prompt = gr.Markdown()
    confirm_box = gr.Textbox(label="Type YES to confirm cancel", visible=False)
    confirm_btn = gr.Button("Confirm Cancel", visible=False)

    cancel_result = gr.Markdown(label="Cancel Result")
    cancel_updated_details = gr.Markdown(label="Updated Order Details")

    ask_btn.click(
        fn=ask_copilot,
        inputs=[message_input, order_id_input, jwt_input],
        outputs=[
            answer_out,
            actions_out,
            order_id_state,
            order_details_btn,
            cancel_order_id_state,
            cancel_btn,
            confirm_box
        ]
    )

    order_details_btn.click(
        fn=open_order_details,
        inputs=[order_id_state, jwt_input],
        outputs=[order_details_out]
    )

    cancel_btn.click(
        fn=request_cancel,
        inputs=[cancel_order_id_state, jwt_input],
        outputs=[cancel_prompt, confirm_box]
    )

    # show confirm button when confirm_box is visible
    confirm_box.change(lambda x: gr.update(visible=True), inputs=confirm_box, outputs=confirm_btn)

    confirm_btn.click(
        fn=confirm_cancel,
        inputs=[cancel_order_id_state, confirm_box, jwt_input],
        outputs=[cancel_result, cancel_updated_details, confirm_box]
    )

app.launch()
