class TestChannel < ApplicationCable::Channel
  def subscribed
    # stream_from "some_channel"
    @currentUserId=Time.new.to_i.to_s
    stream_for @currentUserId
    $redises.sadd('socket_client',@currentUserId)
  end

  def unsubscribed
    logger.info "yuyong-->do_test-->end"+@currentUserId
    $redises.srem('socket_client',@currentUserId)
  end

  def do_test(msg_from_client)
    $redises.smembers('socket_client').each{
      |item|
      if item!=@currentUserId
        TestChannel.broadcast_to(item,to_client_message: "this is a message from server (reponse to message from client \"#{msg_from_client['hello_msg_to_server']}\")")
      end
    }
  end
end
