require 'test_helper'

class TestControllerTest < ActionDispatch::IntegrationTest
  test "should get test_page" do
    get test_test_page_url
    assert_response :success
  end

end
