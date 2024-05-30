const baseUrl = "http://localhost:8080/api/broken-links";

let stompClient = null;

function connect() {
    let socket = new SockJS(
        "http://localhost:8080/logs",
        {},
        { withCredentials: false }
      );
      stompClient = Stomp.over(socket);
    
      stompClient.connect({}, function (frame) {
        console.log("Connected: " + frame);
        stompClient.subscribe("/topic/logs", function (messageOutput) {
          console.log(messageOutput.body);
          addLogToTable(messageOutput.body);
        });
      }, function(error) {
        // This function will be called in case of an error with the connection
        console.error('STOMP error', error);
      });
  }
  
  function addLogToTable(logMessage) {
    const tableBody = document.getElementById("logsTableBody");

    // Create a new row
    const newRow = document.createElement("tr");

    // Create a cell and add the log message to it
    const logCell = document.createElement("td");
    logCell.textContent = logMessage;

    newRow.appendChild(logCell);

    // Add the row to the table
    tableBody.appendChild(newRow);

    const maxTableSize = 10;  // Change this to the maximum number of rows you want
    if (tableBody.rows.length > maxTableSize) {
        tableBody.deleteRow(0);  // Remove the last row if the table size exceeds maxTableSize
    }
}


function submitForm(event) {
  event.preventDefault();

  let searchParams = new URLSearchParams(window.location.search);
  let email = searchParams.get("email");
  let url = document.getElementById("urlInput").value;
  let sendEmail = document.getElementById("sendEmailInput").checked;

  if (!url.startsWith("http://") && !url.startsWith("https://")) {
    url = "http://" + url;
  }
  
  document.getElementById("urlInput").disabled = true;
  document.getElementById("sendEmailInput").disabled = true;
  const table = document.getElementById('logsTable');
  table.style.display = "block"; 

  // Make the API call using the fetch API
  fetch(`${baseUrl}?url=${url}&email=${email}&sendEmail=${sendEmail}`)
    .then((response) => response.json())
    .then((data) => {
      console.log("Response:", data);
      document.getElementById("logsTable").style.display = "none";
      document.getElementById("urlInput").disabled = false;
      document.getElementById("sendEmailInput").disabled = false;
      const report = `
            Total Anchors Tags: ${data.totalAnchors}<br>
            Total Errors: ${data.totalErrors}<br>
            Total Visited Links: ${data.totalVisited}<br>
        `;

        // Display the report
        document.getElementById('report').innerHTML = report;

        const brokenLinksTableBody = document.getElementById('brokenLinksTableBody');
        document.getElementById("tableHeader").hidden = false

        if (Object.keys(data.brokenLinks).length > 0) {
            const brokenLinksTableBody = document.getElementById('brokenLinksTableBody');
            Object.entries(data.brokenLinks).forEach(([url, statusCode]) => {
                const newRow = document.createElement('tr');
                newRow.innerHTML = `<td>${statusCode}</td><td>${url}</td>`;
                brokenLinksTableBody.appendChild(newRow);
            });

            // Display the broken links table
            document.getElementById('brokenLinksTable').style.display = "block";
        } else {
            // Display 'No Broken Links' message
            const noBrokenLinksMsg = document.createElement('p');
            noBrokenLinksMsg.textContent = 'No Broken Links';
            document.getElementById('report').appendChild(noBrokenLinksMsg);
        }
      // Handle the data received after the completion of the crawl process as you need.
    })
    .catch((error) => {
        document.getElementById("urlInput").disabled = false;
        document.getElementById("sendEmailInput").disabled = false;
      console.error("Error:", error);
    });
}

// Connect to the WebSocket when the page loads
window.onload = function() {
    connect();
    const table = document.getElementById('logsTable');
    table.style.display = "none"; // Hide table initially
    document.getElementById('brokenLinksTable').style.display = "none";
  }