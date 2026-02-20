const apiTable = document.getElementById("apiTable");
const btnApply = document.getElementById("btnApply");
const btnDump = document.getElementById("btnDump");

async function loadData() {
  const q = document.getElementById("filterQ").value;
  const from = document.getElementById("filterFrom").value;
  const to = document.getElementById("filterTo").value;
  const limit = document.getElementById("filterLimit").value;

  const params = new URLSearchParams();
  if (q) params.append("q", q);
  if (from) params.append("from", from);
  if (to) params.append("to", to);
  if (limit) params.append("limit", limit);

  const res = await fetch("/api/contact?" + params.toString());
  const data = await res.json();

  apiTable.innerHTML = "";
  data.forEach(item => {
    apiTable.innerHTML += `
      <tr>
        <td>${item.id}</td>
        <td>${item.fullName}</td>
        <td>${item.email}</td>
        <td>${item.phone}</td>
        <td>${new Date(item.createdAt).toLocaleString()}</td>
      </tr>
    `;
  });
}

btnApply.addEventListener("click", loadData);

btnDump.addEventListener("click", () => {
  window.location.href = "/api/contact/dump";
});

loadData();