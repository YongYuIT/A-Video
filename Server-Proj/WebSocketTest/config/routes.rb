Rails.application.routes.draw do
  get 'test/test_page'

  namespace :restful_api do
    post 'user/login'
    post 'user/logout'
    post 'user/report_sid'
  end

  resources :users
end
