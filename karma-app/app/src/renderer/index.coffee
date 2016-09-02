window.onload = ->
  Main = require('./main')
  element = new Main().getElement()
  document.body.appendChild(element)
