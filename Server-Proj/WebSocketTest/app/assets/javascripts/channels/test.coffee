App.test = App.cable.subscriptions.create "TestChannel",
  connected: ->
    alert 'connected'
  disconnected: ->
    alert 'disconnected'
  received: (data) ->
    alert 'client receive message from server' + data["to_client_message"]
  do_test: (hello_mesage_from_client) ->
    @perform 'do_test',hello_msg_to_server:hello_mesage_from_client
