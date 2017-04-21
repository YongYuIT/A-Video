class RestfulApi::UserController < ApplicationController
  skip_before_filter :verify_authenticity_token
  before_action :check_access
  def login
    msg=Login_info.new
    user_name=request.POST["user_name"]
    user_pw=request.POST["user_pwd"]
    current_user=User.where("name = ? AND passwd = ?",user_name,user_pw)
    if current_user[0]==nil
      #login fail
      msg.result_code=1001
      msg.disc="user not found"
      render :text =>  msg.to_json
    return
    else
    current_user=current_user[0]
    end

    tmp_token=$redises.get(current_user.id)

    if tmp_token==nil
      tmp_token=Time.new.to_i.to_s
    $redises.set(current_user.id,tmp_token)
    $redises.set(tmp_token,current_user.id)
    #30min, the token will die
    $redises.expire(tmp_token,60*30)
    $redises.expire(current_user.id,60*30)
    end

    msg.result_code=1002
    msg.token=tmp_token
    render :text =>  msg.to_json
    return
  end

  def logout
    msg=Login_info.new
    token=request.POST["token"]
    id=$redises.get(token)
    if id==nil
      msg.result_code=1001
      msg.disc="user not found or offline"
      render :text =>  msg.to_json
    return
    else
    $redises.del(token)
    $redises.del(id)
    end
    msg.result_code=1002
    msg.disc="logout success"
    render :text =>  msg.to_json
    return
  end

  def report_sid
    msg=Login_info.new
    token=request.POST["token"]
    current_user=get_user(token)
    if current_user==nil
      msg.result_code=1001
      msg.disc="user not found or offline"
      render :text =>  msg.to_json
    return
    end

    $redises.set("sid"+token,request.POST["sid"])
    msg.result_code=1002
    msg.disc="report success"
    render :text =>  msg.to_json
    return

  end
  #############################################################end

  private

  #############################################################start
  def get_user(token)
    id=$redises.get(token)
    if id==nil
      return nil
    else
      if token!=$redises.get(id)
        $redises.del(token)
        return nil;
      end
      $redises.expire(token,60*30)
      $redises.expire(id,60*30)
      return User.find(id)
    end
  end

  def check_access
    msg=Login_info.new
    access_token=request.POST["access_token"]
    if access_token!='1001'
      msg.result_code=1001
      msg.disc="unauthorized"
      render :text =>  msg.to_json
    end
  end
#############################################################end
end

class Login_info
  attr_accessor :disc,:result_code,:token
end
