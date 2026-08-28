document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-dove-password-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const inputId = button.getAttribute('aria-controls');
      const input = inputId ? document.getElementById(inputId) : null;
      if (!(input instanceof HTMLInputElement)) return;

      const visible = input.type === 'text';
      input.type = visible ? 'password' : 'text';
      button.setAttribute('aria-label', visible ? 'Afficher le mot de passe' : 'Masquer le mot de passe');
    });
  });
});

