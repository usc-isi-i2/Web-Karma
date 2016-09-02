
module.exports =
class Main
  constructor: ->
    @element = document.createElement('h1')
    @element.textContent = '🎉 Hello World 🎉'

  getElement: ->
    @element
