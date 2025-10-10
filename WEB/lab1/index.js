let grecaptchaWidgetId;
let isCaptchaVerified = false;

function onRecaptchaLoad() {
    const container = document.getElementById("recaptcha-container");
    if (!container) return;

    grecaptchaWidgetId = grecaptcha.render(container, {
        sitekey: "6LdAVd0rAAAAAA3J6zWCXF4kdi2zDvuN9Mxk0YkV",
        callback: onCaptchaSuccess,
        "expired-callback": onCaptchaExpired,
        "error-callback": onCaptchaError
    });
}

function onCaptchaSuccess(token) {
    console.log("reCAPTCHA passed:", token);
    isCaptchaVerified = true;
}

function onCaptchaExpired() {
    isCaptchaVerified = false;
    console.warn("reCAPTCHA expired");
}

function onCaptchaError() {
    isCaptchaVerified = false;
    console.error("reCAPTCHA error");
    showError("Ошибка капчи. Перезагрузите страницу.");
}


document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("data-form");
    const table = document.getElementById("result-table");
    const errorBox = document.getElementById("error");
    const yInput = document.getElementById("y");
    const xSelect = document.getElementById("x");
    const rButtons = document.querySelectorAll('button[name="r"]');
    const canvas = document.getElementById("graph");
    const ctx = canvas.getContext("2d");

    const rHiddenInput = document.createElement("input");
    rHiddenInput.type = "hidden";
    rHiddenInput.name = "r";
    rHiddenInput.id = "r-value";
    form.appendChild(rHiddenInput);

    const state = {
        x: 0,
        y: 0,
        r: 1.0,
    };

    const VALID_X_VALUES = new Set([-3, -2, -1, 0, 1, 2, 3, 4, 5]);
    const VALID_R_VALUES = new Set([1.0, 1.5, 2.0, 2.5, 3.0]);
    const Y_MIN = -3;
    const Y_MAX = 3;

    function showError(message) {
        errorBox.innerText = message;
        errorBox.hidden = false;
    }

    function clearError() {
        errorBox.hidden = true;
    }

    function isValidNumber(str) {
        return /^[-+]?(\d+([.,]\d*)?|[.,]\d+)$/.test(str.trim());
    }

    function validate() {
        const x = parseFloat(xSelect.value);
        const yStr = yInput.value.trim();

        if (!isValidNumber(yStr)) {
            showError("Y must be a valid number (e.g. 1.5 or 1,5)");
            return false;
        }

        const y = parseFloat(yStr.replace(',', '.'));
        const r = state.r;

        if (!VALID_X_VALUES.has(x)) {
            showError(`X must be one of: ${[...VALID_X_VALUES].sort((a,b)=>a-b).join(", ")}`);
            return false;
        }

        if (isNaN(y) || y < Y_MIN || y > Y_MAX) {
            showError(`Y must be between ${Y_MIN} and ${Y_MAX}`);
            return false;
        }

        if (!VALID_R_VALUES.has(r)) {
            showError(`R must be one of: ${[...VALID_R_VALUES].join(", ")}`);
            return false;
        }

        state.x = x;
        state.y = y;
        state.r = r;

        clearError();
        return true;
    }

    rButtons.forEach(button => {
        button.addEventListener("click", () => {
            const r = parseFloat(button.value);
            if (VALID_R_VALUES.has(r)) {
                state.r = r;
                rHiddenInput.value = r;

                rButtons.forEach(b => b.classList.remove("selected"));
                button.classList.add("selected");

                drawGraph(state.r);
            }
        });
    });

    xSelect.addEventListener("change", () => {
        const x = parseFloat(xSelect.value);
        if (VALID_X_VALUES.has(x)) {
            state.x = x;
        }
    });

    yInput.addEventListener("input", () => {
        const y = yInput.value.trim();
        state.y = y === "" ? 0 : y;
    });

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!validate()) return;

        if (!isCaptchaVerified) {
            showError("Пожалуйста, подтвердите, что вы не робот.");
            return;
        }

        try {
            const params = new URLSearchParams({
                x: state.x,
                y: yInput.value,
                r: state.r,
                captcha: grecaptcha.getResponse(grecaptchaWidgetId)
            });

            const response = await fetch(`/fcgi-bin/app.jar?${params}`, { method: 'GET' });
            const data = await response.json();

            const resultText = data.result !== undefined 
                ? (data.result ? "Hit" : "Miss") 
                : "Error";

            addRowToTable({
                x: state.x,
                y: state.y,
                r: state.r,
                time: data.now ? new Date(data.now).toLocaleString() : "—",
                execTime: data.time ? `${data.time} ns` : "—",
                result: resultText
            });

            saveToHistory();
            drawGraph(state.r);

            grecaptcha.reset(grecaptchaWidgetId);
            isCaptchaVerified = false;

        } catch (err) {
            console.error(err);
            addRowToTable({
                x: state.x,
                y: state.y,
                r: state.r,
                time: "—",
                execTime: "—",
                result: "Network error"
            });
            grecaptcha.reset(grecaptchaWidgetId);
        }
    });

    function addRowToTable(data) {
        const row = table.insertRow();
        Object.values(data).forEach(text => {
            const cell = row.insertCell();
            cell.textContent = text;
        });
    }

    function saveToHistory() {
        const history = JSON.parse(localStorage.getItem("results") || "[]");
        history.push({ 
            ...state, 
            timestamp: new Date().toISOString(),
            time: new Date().toLocaleString(),
            execTime: "—" 
        });
        localStorage.setItem("results", JSON.stringify(history));
    }

    function loadHistory() {
        const history = JSON.parse(localStorage.getItem("results") || "[]");
        history.forEach(entry => {
            addRowToTable({
                x: entry.x,
                y: entry.y,
                r: entry.r,
                time: entry.time || "—",
                execTime: entry.execTime || "—",
                result: "Loaded"
            });
        });
    }

    function drawGraph(R) {
        if (!R) R = 1.0;
        const scale = 80;
        const w = canvas.width;
        const h = canvas.height;
        const cx = w / 2;
        const cy = h / 2;

        ctx.clearRect(0, 0, w, h);
        ctx.fillStyle = "#f0f0f0";
        ctx.fillRect(0, 0, w, h);

        ctx.strokeStyle = "#333";
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(cx, 0);
        ctx.lineTo(cx, h);
        ctx.moveTo(0, cy);
        ctx.lineTo(w, cy);
        ctx.stroke();

        ctx.fillStyle = "#000";
        ctx.font = "12px Arial";

        [-2, -1, 0, 1, 2].forEach(val => {
            const pos = val * scale;
            if (val !== 0) {
                ctx.fillText(val * R, cx + pos - 4, cy + 12);
                ctx.fillRect(cx + pos - 1, cy - 3, 2, 6);
            }
            ctx.fillText(val * R, cx + 4, cy - pos + 4);
            ctx.fillRect(cx - 3, cy - pos - 1, 6, 2);
        });

        ctx.fillStyle = "rgba(76, 175, 80, 0.3)";
        ctx.fillRect(cx, cy, (1 * scale) / 2, -1 * scale);

        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(cx - 1 * scale, cy);
        ctx.lineTo(cx, cy - 1 * scale / 2);
        ctx.closePath();
        ctx.fill();

        ctx.beginPath();
        ctx.arc(cx, cy, 1 * scale, 0, Math.PI / 2);
        ctx.lineTo(cx, cy);
        ctx.closePath();
        ctx.fill();
    }

    if (rButtons.length > 0) {
        rButtons[0].classList.add("selected");
        state.r = parseFloat(rButtons[0].value);
        rHiddenInput.value = state.r;
    }

    loadHistory();
    drawGraph(state.r);
});
