class GroupChatChannel < ApplicationCable::Channel
  def subscribed
    @currentUserId=Time.new.to_i.to_s
    stream_for @currentUserId
    logger.info "yuyong--join--"+@currentUserId
    $redises.sadd('gropu_chat_clients',@currentUserId)
  end

  def unsubscribed
    logger.info "yuyong--leave--"+@currentUserId
    $redises.srem('gropu_chat_clients',@currentUserId)
  end

  def on_message(message)
    logger.info "yuyong--on_message--"+message['message_get']
    contents=message['message_get'].split('####')
    if contents[0]=='report_sdp'
      on_report_sdp(contents[1])
    end
    if contents[0]=='answer_sdp'
      on_answer_sdp(contents[1])
    end
    if contents[0]=='report_params'
      on_report_params(contents[1])
    end
    if contents[0]=='answer_params'
      on_answer_params(contents[1])
    end
  end

  private
  
  def on_answer_params(params)
    current_index=$redises.smembers('gropu_chat_ready_clients').index(@currentUserId)
    last_id=$redises.smembers('gropu_chat_ready_clients')[(current_index-1)%3]
    GroupChatChannel.broadcast_to(last_id,message_to_client: "set_params####"+params)
    logger.info "yuyong--on_answer_params--"+@currentUserId+"-->"+last_id
  end
  
  def on_report_params(params)
    current_index=$redises.smembers('gropu_chat_ready_clients').index(@currentUserId)
    next_id=$redises.smembers('gropu_chat_ready_clients')[(current_index+1)%3]
    GroupChatChannel.broadcast_to(next_id,message_to_client: "get_params####"+params)
    logger.info "yuyong--on_report_params--"+@currentUserId+"-->"+next_id
  end
  
  def on_answer_sdp(sdp)
    current_index=$redises.smembers('gropu_chat_ready_clients').index(@currentUserId)
    last_id=$redises.smembers('gropu_chat_ready_clients')[(current_index-1)%3]
    GroupChatChannel.broadcast_to(last_id,message_to_client: "set_remote####"+sdp)
    logger.info "yuyong--on_report_answer_sdp--"+@currentUserId+"-->"+last_id
  end

  def on_report_sdp(sdp)
    $redises.sadd('gropu_chat_ready_clients',@currentUserId)
    $redises.set(@currentUserId+"####call_sdp",sdp)
    if $redises.smembers('gropu_chat_ready_clients').size == 3
      for i in 0..2
        current_sdp=$redises.get($redises.smembers('gropu_chat_ready_clients')[i]+"####call_sdp")
        next_id=$redises.smembers('gropu_chat_ready_clients')[(i+1)%3]
        GroupChatChannel.broadcast_to(next_id,message_to_client: "get_remote####"+current_sdp)
        logger.info "yuyong--on_report_sdp--"+$redises.smembers('gropu_chat_ready_clients')[i]+"-->"+next_id
      end
    end
  end
end
