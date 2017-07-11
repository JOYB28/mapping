"""mapping URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.11/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url, include
from django.contrib import admin

from .views import UserList, UserDetail, UserPhotoList, UserFriendsList, PhotoList, PhotoDetail

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^auth/', include('rest_framework_social_oauth2.urls')),
    url(r'^api/users$', UserList.as_view()),
    url(r'^api/users/(?P<user>[0-9]+)$', UserDetail.as_view()),
    url(r'^api/users/(?P<user>[0-9]+)/photos$', UserPhotoList.as_view()),
    url(r'^api/users/(?P<user>[0-9]+)/friends$', UserFriendsList.as_view()),
    url(r'^api/(?P<user>me)$', UserDetail.as_view()),
    url(r'^api/(?P<user>me)/photos$', UserPhotoList.as_view()),
    url(r'^api/(?P<user>me)/friends$', UserFriendsList.as_view()),
    url(r'^api/photos$', PhotoList.as_view()),
    url(r'^api/photos/(?P<photo>[0-9]+)$', PhotoDetail.as_view()),
]
