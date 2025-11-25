document.addEventListener("DOMContentLoaded", () => {
  fetch('/identityiq/plugin/rest/TodoPlugin/dbdata')
    .then(response => response.json())
    .then(data => {
      const tbody = document.querySelector('#todoTable tbody');
      data.forEach(row => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${row.id}</td><td>${row.name}</td>`;
        tbody.appendChild(tr);
      });
    })
    .catch(err => console.error('Error fetching data:', err));
});
