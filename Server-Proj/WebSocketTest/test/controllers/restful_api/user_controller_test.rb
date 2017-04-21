require 'test_helper'

class RestfulApi::UserControllerTest < ActionDispatch::IntegrationTest
  test "should get login" do
    get restful_api_user_login_url
    assert_response :success
  end

end
