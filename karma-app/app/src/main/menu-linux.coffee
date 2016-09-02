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
          accelerator: 'Control+H',
          selector: 'hide:'
        },
        {
          label: 'Hide Others',
          accelerator: 'Control+Shift+H',
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
          accelerator: 'Control+Q',
          click: -> app.quit()
        }
      ]
    },
    {
      label: 'File',
      submenu: [
        {
          label: 'Open…',
          accelerator: 'Control+o',
          click: -> global.application.openDialog()
        },
        {
          type: 'separator'
        },
        {
          label: 'Close Window',
          accelerator: 'Control+W',
          click: -> window.close()
        }
      ]
    },
    {
      label: 'View',
      submenu: [
        {
          label: 'Reload',
          accelerator: 'Control+R',
          click: -> window.restart()
        },
        {
          label: 'Toggle Full Screen',
          accelerator: 'Control+Shift+F',
          click: -> window.setFullScreen(!window.isFullScreen())
        },
        {
          label: 'Toggle Developer Tools',
          accelerator: 'Alt+Control+I',
          click: -> window.toggleDevTools()
        }
      ]
    },
    {
      label: 'Window',
      submenu: [
        {
          label: 'Minimize',
          accelerator: 'Control+M',
          selector: 'performMiniaturize:'
        },
        {
          label: 'Close',
          accelerator: 'Control+W',
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
