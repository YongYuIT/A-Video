App.group_chat = App.cable.subscriptions.create "GroupChatChannel",
	connected: ->
		alert 'connected'
	disconnected: ->
		alert 'disconnected'
	received: (data) ->
		alert data["message_to_client"]
	send_message: (message_to_server) ->
		@perform 'on_message',message_get:message_to_server
    
