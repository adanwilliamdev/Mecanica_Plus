(function () {
  'use strict';

  // Confirmação antes de ações destrutivas: <form data-confirm="Tem certeza?">
  document.addEventListener('submit', function (event) {
    var message = event.target.getAttribute('data-confirm');
    if (message && !window.confirm(message)) {
      event.preventDefault();
    }
  });

  // Menu lateral no mobile.
  var toggle = document.querySelector('.menu-toggle');
  if (toggle) {
    toggle.addEventListener('click', function () {
      document.body.classList.toggle('nav-open');
    });
  }

  // Filtra as opções de veículo pelo cliente escolhido:
  // <select id="clientId"> + <select data-vehicle-select> com <option data-client="...">.
  var client = document.getElementById('clientId');
  var vehicle = document.querySelector('[data-vehicle-select]');
  if (client && vehicle) {
    var options = Array.prototype.slice.call(vehicle.querySelectorAll('option[data-client]'));

    var apply = function (reset) {
      var selected = client.value;
      options.forEach(function (option) {
        var visible = selected !== '' && option.getAttribute('data-client') === selected;
        option.hidden = !visible;
        option.disabled = !visible;
      });
      var current = vehicle.options[vehicle.selectedIndex];
      if (reset || !current || current.disabled) {
        vehicle.value = '';
      }
      vehicle.disabled = selected === '';
    };

    client.addEventListener('change', function () { apply(true); });
    apply(false);
  }
})();
