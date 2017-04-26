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
    $redises.srem('socket_client','')
    $redises.smembers('socket_client').each{
      |item|
      if item!=@currentUserId
        msg_to_client=msg_from_client['hello_msg_to_server']
        TestChannel.broadcast_to(item,to_client_message: msg_to_client )
      end
    }
  end
end
