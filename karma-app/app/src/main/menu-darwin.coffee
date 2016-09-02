module.exports = (app, window) ->
  [
    {
      label: 'App',
      submenu: [
        {
          label: 'About',
          selector: 'orderFrontStandardAboutPanel:'
        },
        {
          type: 'separator'
        },
        {
          label: 'Hide',
          accelerator: 'Command+H',
          selector: 'hide:'
        },
        {
          label: 'Hide Others',
          accelerator: 'Command+Shift+H',
          selector: 'hideOtherApplications:'
        },
        {
          label: 'Show All',
          selector: 'unhideAllApplications:'
        },
        {
          type: 'separator'
        },
        {
          label: 'Quit',
          accelerator: 'Command+Q',
          click: -> app.quit()
        }
      ]
    },
    {
      label: 'File',
      submenu: [
        {
          label: 'Open…',
          accelerator: 'Command+o',
          click: -> global.application.openDialog()
        },
        {
          type: 'separator'
        },
        {
          label: 'Close Window',
          accelerator: 'Command+W',
          click: -> window.close()
        }
      ]
    },
    {
      label: 'View',
      submenu: [
        {
          label: 'Reload',
          accelerator: 'Command+R',
          click: -> window.restart()
        },
        {
          label: 'Toggle Full Screen',
          accelerator: 'Command+Shift+F',
          click: -> window.setFullScreen(!window.isFullScreen())
        },
        {
          label: 'Toggle Developer Tools',
          accelerator: 'Alt+Command+I',
          click: -> window.toggleDevTools()
        }
      ]
    },
    {
      label: 'Window',
      submenu: [
        {
          label: 'Minimize',
          accelerator: 'Command+M',
          selector: 'performMiniaturize:'
        },
        {
          label: 'Close',
          accelerator: 'Command+W',
          selector: 'performClose:'
        },
        {
          type: 'separator'
        },
        {
          label: 'Bring All to Front',
          selector: 'arrangeInFront:'
        }
      ]
    },
    {
      label: 'Help',
      submenu: [
        {
          label: 'Repository',
          click: -> require('shell').openExternal('http://github.com/benogle/electron-sample')
        }
      ]
    }
  ]
