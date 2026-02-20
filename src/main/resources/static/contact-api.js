(function () {
  const form = document.getElementById('contactForm');
  const clientError = document.getElementById('clientError');

  const apiTable = document.getElementById('apiTable');
  const apiStatus = document.getElementById('apiStatus');

  const btnRefresh = document.getElementById('btnRefresh');
  const btnDump = document.getElementById('btnDump');
  const btnApply = document.getElementById('btnApply');
  const btnClear = document.getElementById('btnClear');

  const filterQ = document.getElementById('filterQ');
  const filterFrom = document.getElementById('filterFrom');
  const filterTo = document.getElementById('filterTo');
  const filterLimit = document.getElementById('filterLimit');

  if (!form) return;

  const fullName = document.getElementById('fullName');
  const email = document.getElementById('email');
  const phone = document.getElementById('phone');
  const birthDate = document.getElementById('birthDate');
  const message = document.getElementById('message');

  function showClientError(msg){
    if (!clientError) return alert(msg);
    clientError.textContent = msg;
    clientError.style.display = 'block';
  }
  function clearClientError(){
    if (!clientError) return;
    clientError.textContent = '';
    clientError.style.display = 'none';
  }

  function setStatus(msg) {
    if (!apiStatus) return;
    apiStatus.textContent = msg || '';
  }

  function hasBadChars(s){
    if (!s) return false;
    if (s.includes('<') || s.includes('>')) return true;
    return /[\u0000-\u001F\u007F]/.test(s);
  }

  function normalizeSpaces(s){
    return (s || '').trim().replace(/\s+/g, ' ');
  }

  function escapeHtml(s) {
    return String(s ?? '')
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function formatInstant(iso) {
    // iso: "2026-02-19T..."
    try {
      const d = new Date(iso);
      if (isNaN(d.getTime())) return iso;
      return d.toLocaleString();
    } catch {
      return iso;
    }
  }

  // Limpieza (cliente)
  [fullName, email, phone, message].forEach(el => {
    if (!el) return;

    el.addEventListener('input', () => {
      clearClientError();
      el.value = el.value.replace(/[\u0000-\u001F\u007F]/g, '');
    });

    el.addEventListener('blur', () => {
      el.value = normalizeSpaces(el.value);
    });

    el.addEventListener('paste', (e) => {
      const text = (e.clipboardData || window.clipboardData).getData('text');
      if (hasBadChars(text)) {
        e.preventDefault();
        showClientError("No se permiten etiquetas HTML ni caracteres extraños.");
      }
    });
  });

  function buildListUrl() {
    const params = new URLSearchParams();
    const q = (filterQ?.value || '').trim();
    const from = (filterFrom?.value || '').trim();
    const to = (filterTo?.value || '').trim();
    const limit = (filterLimit?.value || '5').trim();

    if (q) params.set('q', q);
    if (from) params.set('from', from);
    if (to) params.set('to', to);
    if (limit) params.set('limit', limit);

    const qs = params.toString();
    return '/api/contact' + (qs ? `?${qs}` : '');
  }

  async function refreshList() {
    if (!apiTable) return;

    try {
      clearClientError();
      setStatus('Cargando lista...');

      const url = buildListUrl();
      const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
      if (!res.ok) throw new Error('No se pudo cargar la lista');

      const items = await res.json();

      apiTable.innerHTML = '';
      items.forEach(it => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td style="padding:8px 6px; border-bottom:1px solid rgba(255,255,255,.08)">${it.id}</td>
          <td style="padding:8px 6px; border-bottom:1px solid rgba(255,255,255,.08)">${escapeHtml(it.fullName)}</td>
          <td style="padding:8px 6px; border-bottom:1px solid rgba(255,255,255,.08)">${escapeHtml(it.email)}</td>
          <td style="padding:8px 6px; border-bottom:1px solid rgba(255,255,255,.08)">${escapeHtml(it.phone)}</td>
          <td style="padding:8px 6px; border-bottom:1px solid rgba(255,255,255,.08)">${escapeHtml(it.birthDate)}</td>
          <td style="padding:8px 6px; border-bottom:1px solid rgba(255,255,255,.08)">${escapeHtml(formatInstant(it.createdAt))}</td>
        `;
        apiTable.appendChild(tr);
      });

      setStatus(items.length ? `Mostrando ${items.length} registro(s).` : 'Sin registros.');
    } catch (e) {
      setStatus('');
      showClientError(e.message || 'Error al cargar la lista');
    }
  }

  // Botones de filtros
  if (btnApply) btnApply.addEventListener('click', refreshList);
  if (btnRefresh) btnRefresh.addEventListener('click', refreshList);

  if (btnClear) {
    btnClear.addEventListener('click', () => {
      if (filterQ) filterQ.value = '';
      if (filterFrom) filterFrom.value = '';
      if (filterTo) filterTo.value = '';
      if (filterLimit) filterLimit.value = '5';
      refreshList();
    });
  }

  // Dump
  if (btnDump) {
    btnDump.addEventListener('click', async () => {
      try {
        clearClientError();
        setStatus('Generando dump...');

        const res = await fetch('/api/contact/dump');
        if (!res.ok) throw new Error('No se pudo generar el dump');

        const blob = await res.blob();
        const dispo = res.headers.get('content-disposition') || '';
        const match = dispo.match(/filename=([^;]+)/i);
        const filename = match ? match[1].replaceAll('"', '').trim() : 'contact_messages_dump.json';

        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        URL.revokeObjectURL(url);

        setStatus('Dump descargado.');
      } catch (e) {
        setStatus('');
        showClientError(e.message || 'Error al descargar dump');
      }
    });
  }

  // Submit por Fetch API
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearClientError();

    const values = [
      fullName?.value || '',
      email?.value || '',
      phone?.value || '',
      message?.value || ''
    ];

    if (values.some(hasBadChars)) {
      showClientError("No se permiten etiquetas HTML ni caracteres extraños.");
      return;
    }

    if (!form.checkValidity()) {
      showClientError("Revisa los campos: formato inválido o faltan datos.");
      return;
    }

    if (fullName) fullName.value = normalizeSpaces(fullName.value);
    if (email) email.value = normalizeSpaces(email.value);
    if (phone) phone.value = normalizeSpaces(phone.value);
    if (message) message.value = normalizeSpaces(message.value);

    const payload = {
      fullName: (fullName?.value || '').trim(),
      email: (email?.value || '').trim(),
      phone: (phone?.value || '').trim(),
      birthDate: (birthDate?.value || '').trim() || null,
      message: (message?.value || '').trim()
    };

    try {
      setStatus('Enviando...');

      const res = await fetch('/api/contact', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await res.json().catch(() => ({}));
      if (!res.ok) {
        const errs = data?.errors || {};
        const msg =
          errs.global ||
          errs.fullName ||
          errs.email ||
          errs.phone ||
          errs.birthDate ||
          errs.message ||
          'Datos inválidos';
        throw new Error(msg);
      }

      setStatus('✓ Guardado por Fetch API');
      form.reset();
      await refreshList();
    } catch (err) {
      setStatus('');
      showClientError(err?.message || 'Error al enviar');
    }
  });

  // Auto-load
  refreshList();
})();