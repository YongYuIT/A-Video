App.test = App.cable.subscriptions.create "TestChannel",
  connected: ->
    # Called when the subscription is ready for use on the server

  disconnected: ->
    # Called when the subscription has been terminated by the server

  received: (data) ->
    # Called when there's incoming data on the websocket for this channel
    alert 'client receive message from server' + data["to_client_message"]
  do_test: (hello_mesage_from_client) ->
    @perform 'do_test',hello_msg_to_server:hello_mesage_from_client
