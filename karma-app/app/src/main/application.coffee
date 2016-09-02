ApplicationWindow = require './application-window'
app = require('app') # provided by electron

module.exports =
class Application
  window: null

  constructor: (options) ->
    global.application = this

    # Report crashes to our server.
    require('crash-reporter').start()

    # Quit when all windows are closed.
    app.on 'window-all-closed', -> app.quit()
    app.on 'ready', => @openWindow()

  openWindow: ->
    htmlURL = "file://#{__dirname}/../renderer/index.html"
    @window = new ApplicationWindow htmlURL,
      width: 1200,
      height: 800
