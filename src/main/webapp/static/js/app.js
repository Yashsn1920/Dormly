(() => {
  "use strict";

  const dialog = document.querySelector("[data-confirm-dialog]");
  const dialogMessage = dialog?.querySelector("[data-confirm-message]");
  const confirmButton = dialog?.querySelector("[data-confirm-submit]");
  let pendingForm = null;

  document.querySelectorAll("form[data-confirm]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      if (!dialog || !dialogMessage || !confirmButton || form.dataset.confirmed === "true") return;
      event.preventDefault();
      pendingForm = form;
      dialogMessage.textContent = form.dataset.confirm || "Are you sure you want to continue?";
      dialog.showModal();
    });
  });

  confirmButton?.addEventListener("click", () => {
    if (!pendingForm) return;
    const form = pendingForm;
    pendingForm = null;
    dialog.close();
    form.dataset.confirmed = "true";
    form.requestSubmit();
  });

  dialog?.querySelectorAll("[data-confirm-cancel]").forEach((button) => {
    button.addEventListener("click", () => {
      pendingForm = null;
      dialog.close();
    });
  });

  document.querySelectorAll('input[type="file"]').forEach((input) => {
    const label = input.parentElement?.querySelector("[data-file-name]");
    input.addEventListener("change", () => {
      if (label) label.textContent = input.files?.[0]?.name || "No file selected";
    });
  });

  document.querySelectorAll("textarea[maxlength]").forEach((textarea) => {
    let counter = textarea.parentElement?.querySelector("[data-char-count]");
    if (!counter && textarea.parentElement) {
      counter = document.createElement("small");
      counter.className = "field-hint";
      counter.setAttribute("data-char-count", "");
      textarea.parentElement.append(counter);
    }
    const update = () => {
      if (counter) counter.textContent = `${textarea.value.length} / ${textarea.maxLength}`;
    };
    textarea.addEventListener("input", update);
    update();
  });
})();
