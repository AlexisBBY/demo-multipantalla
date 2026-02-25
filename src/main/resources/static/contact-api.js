const apiTable = document.getElementById("apiTable");
const btnApply = document.getElementById("btnApply");
const btnClear = document.getElementById("btnClear");
const btnDump = document.getElementById("btnDump");

const form = document.getElementById("contactForm");
const clientError = document.getElementById("clientError");

// Inputs form
const fullName = document.getElementById("fullName");
const email = document.getElementById("email");
const phone = document.getElementById("phone");
const birthDate = document.getElementById("birthDate");
const message = document.getElementById("message");

// Inputs filtros
const filterQ = document.getElementById("filterQ");
const filterFrom = document.getElementById("filterFrom");
const filterTo = document.getElementById("filterTo");
const filterLimit = document.getElementById("filterLimit");

function showError(msg) {
  clientError.style.display = "block";
  clientError.textContent = msg;
}

function clearError() {
  clientError.style.display = "none";
  clientError.textContent = "";
}

function setInvalid(el, isInvalid) {
  if (!el) return;
  if (isInvalid) el.classList.add("is-invalid");
  else el.classList.remove("is-invalid");
}

function validateForm() {
  clearError();

  const nameVal = fullName.value.trim();
  const emailVal = email.value.trim();
  const phoneVal = phone.value.trim();
  const birthVal = birthDate.value.trim();
  const msgVal = message.value.trim();

  let ok = true;

  // reset invalid
  [fullName, email, phone, birthDate, message].forEach(i => setInvalid(i, false));

  if (nameVal.length < 3) { setInvalid(fullName, true); ok = false; }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailVal)) { setInvalid(email, true); ok = false; }
  if (!/^[0-9+()\- \s]{7,20}$/.test(phoneVal)) { setInvalid(phone, true); ok = false; }
  if (!birthVal) { setInvalid(birthDate, true); ok = false; }
  if (msgVal.length < 5) { setInvalid(message, true); ok = false; }
  if (msgVal.length > 500) { setInvalid(message, true); ok = false; }

  if (!ok) {
    showError("Revisa los campos marcados en rojo. Todos son obligatorios y deben tener formato válido.");
  }

  return ok;
}

async function loadData() {
  const q = filterQ.value.trim();
  const from = filterFrom.value.trim();
  const to = filterTo.value.trim();
  const limit = filterLimit.value;

  const params = new URLSearchParams();
  if (q) params.append("q", q);
  if (from) params.append("from", from);
  if (to) params.append("to", to);
  if (limit) params.append("limit", limit);

  const res = await fetch("/api/contact?" + params.toString());
  if (!res.ok) {
    apiTable.innerHTML = `<tr><td colspan="6">Error cargando datos (${res.status})</td></tr>`;
    return;
  }

  const data = await res.json();

  apiTable.innerHTML = "";
  if (!data.length) {
    apiTable.innerHTML = `<tr><td colspan="6">Sin resultados</td></tr>`;
    return;
  }

  data.forEach(item => {
    apiTable.innerHTML += `
      <tr>
        <td>${item.id ?? ""}</td>
        <td>${item.fullName ?? ""}</td>
        <td>${item.email ?? ""}</td>
        <td>${item.phone ?? ""}</td>
        <td>${item.birthDate ?? ""}</td>
        <td>${item.createdAt ? new Date(item.createdAt).toLocaleString() : ""}</td>
      </tr>
    `;
  });
}

// Filtros
btnApply.addEventListener("click", loadData);

btnClear.addEventListener("click", () => {
  filterQ.value = "";
  filterFrom.value = "";
  filterTo.value = "";
  filterLimit.value = "5";
  loadData();
});

// Buscar con Enter en filtros
[filterQ, filterFrom, filterTo].forEach(inp => {
  inp.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      loadData();
    }
  });
});

// Dump JSON (descarga)
btnDump.addEventListener("click", () => {
  window.location.href = "/api/contact/dump";
});

// Crear registro con Fetch (POST)
form.addEventListener("submit", async (e) => {
  e.preventDefault();

  if (!validateForm()) return;

  const payload = {
    fullName: fullName.value.trim(),
    email: email.value.trim(),
    phone: phone.value.trim(),
    birthDate: birthDate.value,
    message: message.value.trim()
  };

  try {
    const res = await fetch("/api/contact", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      // Intentar leer error del server
      let msg = `No se pudo guardar (${res.status}).`;
      try {
        const t = await res.text();
        if (t) msg += " " + t;
      } catch (_) {}
      showError(msg);
      return;
    }

    // OK
    clearError();
    form.reset();
    await loadData();
    alert("✅ Registro guardado correctamente");
  } catch (err) {
    showError("Error de red: " + err.message);
  }
});

// Carga inicial
loadData();