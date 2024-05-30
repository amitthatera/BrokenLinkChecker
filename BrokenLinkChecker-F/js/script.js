document.addEventListener("DOMContentLoaded", function () {
    const emailInput = document.getElementById("emailInput");
    const nextButton = document.getElementById("nextButton");
  
    nextButton.addEventListener("click", function (event) {
      event.preventDefault();
  
      // Get the value entered in the email input field
      const email = emailInput.value;
  
      // Navigate to search.html with the updated email as a query parameter
      window.location.href = `checker.html?email=${encodeURIComponent(email)}`;
    });
  });