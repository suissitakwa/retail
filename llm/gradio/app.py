import os
import requests
import gradio as gr
from dotenv import load_dotenv
from typing import Optional

load_dotenv()

BACKEND_URL = os.getenv("RETAIL_BACKEND_URL", "http://localhost:8080")

def normalize_token(jwt_token: str) -> str:
    token = (jwt_token or "").strip()
    if token.lower().startswith("bearer "):
        token = token[7:].strip()
    return token

def backend_post_copilot(message: str, order_id: Optional[str], jwt_token: str):
    token = normalize_token(jwt_token)
    if not token:
        return {"error": "Please paste your JWT token."}

    payload = {"message": message}
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

def backend_get_order_details(order_id: int, jwt_token: str):
    token = normalize_token(jwt_token)
    headers = {"Authorization": f"Bearer {token}"}
    r = requests.get(f"{BACKEND_URL}/api/v1/orders/{order_id}", headers=headers, timeout=30)
    if r.status_code in (401, 403):
        return {"error": f"Auth failed ({r.status_code})."}
    if not r.ok:
        return {"error": f"Backend error ({r.status_code}): {r.text}"}
    return r.json()

def render_actions(actions: list) -> str:
    """Show actions as a small checklist (buttons handled separately)."""
    if not actions:
        return "_No actions_"
    lines = []
    for a in actions:
        name = a.get("name", "ACTION")
        summary = a.get("outputSummary", "")
        lines.append(f"- **{name}** — {summary}")
    return "\n".join(lines)

# --- Gradio handlers ---

def ask_copilot(message: str, order_id: str, jwt_token: str):
    resp = backend_post_copilot(message, order_id, jwt_token)

    if isinstance(resp, dict) and resp.get("error"):
        return resp["error"], "_No actions_", None, gr.update(visible=False)

    answer = resp.get("answer", "")
    actions = resp.get("actions", [])
    actions_md = render_actions(actions)


    order_details_btn_visible = False
    order_id_for_action = None

    for a in actions:
        if a.get("name") == "OPEN_ORDER_DETAILS":
            inp = a.get("input") or {}
            oid = inp.get("orderId")
            if isinstance(oid, int):
                order_details_btn_visible = True
                order_id_for_action = oid
                break

    return answer, actions_md, order_id_for_action, gr.update(visible=order_details_btn_visible)

def open_order_details(order_id_for_action, jwt_token: str):
    if not order_id_for_action:
        return "No orderId found."
    data = backend_get_order_details(int(order_id_for_action), jwt_token)
    if isinstance(data, dict) and data.get("error"):
        return data["error"]


    return f"```json\n{data}\n```"

# --- UI ---

with gr.Blocks(title="Retail Support Copilot (Gradio)") as app:
    gr.Markdown("## 🛍️ Retail Support Copilot (Gradio Demo)\nTalk to your Retail backend via `/api/v1/copilot/chat`.")

    with gr.Row():
        jwt_input = gr.Textbox(label="JWT Token", placeholder="Paste JWT (with or without 'Bearer ')", type="password")
    with gr.Row():
        message_input = gr.Textbox(label="Message", placeholder="Where is my order?", lines=2)
        order_id_input = gr.Textbox(label="Optional orderId", placeholder="752", max_lines=1)

    ask_btn = gr.Button("Ask Copilot")

    answer_out = gr.Markdown(label="Answer")
    actions_out = gr.Markdown(label="Actions")

    # hidden state to store orderId for action
    order_id_state = gr.State(None)

    order_details_btn = gr.Button("Open Order Details", visible=False)
    order_details_out = gr.Markdown(label="Order Details")

    ask_btn.click(
        fn=ask_copilot,
        inputs=[message_input, order_id_input, jwt_input],
        outputs=[answer_out, actions_out, order_id_state, order_details_btn]
    )

    order_details_btn.click(
        fn=open_order_details,
        inputs=[order_id_state, jwt_input],
        outputs=[order_details_out]
    )

app.launch()
