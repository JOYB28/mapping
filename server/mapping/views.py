from django.contrib.auth.models import User
from rest_framework import serializers, status
from rest_framework.generics import ListCreateAPIView, ListAPIView, RetrieveAPIView
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from social_django.models import UserSocialAuth
import requests

from .models import UserProfile, Photo, get_friends


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'username', 'email')


class PhotoSerializer(serializers.ModelSerializer):
    user = UserSerializer()

    class Meta:
        model = Photo
        fields = '__all__'


class PhotoList(ListAPIView):
    queryset = Photo.objects.all()
    serializer_class = PhotoSerializer
    permission_classes = (IsAuthenticated,)

    def get_queryset(self):
        queryset = super().get_queryset()

        is_public = self.request.query_params.get('show') == 'all'
        if is_public:
            queryset = queryset.accessible_for(self.request.user)
        else:
            queryset = queryset.filter_friends(self.request.user)

        grid = self.request.query_params.get('grid')
        if grid:
            queryset = queryset.filter(grid=grid)
        return queryset


class PhotoDetail(RetrieveAPIView):
    queryset = Photo.objects.all()
    serializer_class = PhotoSerializer
    permission_classes = (IsAuthenticated,)
    lookup_url_kwarg = 'photo'

    def get_queryset(self):
        queryset = super().get_queryset()
        return queryset.accessible_for(self.request.user)


class UserList(ListAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (IsAuthenticated,)


class UserDetail(RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (IsAuthenticated,)
    lookup_url_kwarg = 'user'

    def get_object(self):
        if self.kwargs.get(self.lookup_url_kwarg) == 'me' and self.request.user.is_authenticated():
            self.kwargs[self.lookup_url_kwarg] = self.request.user.pk
        return super().get_object()


class UserPhotoList(ListCreateAPIView):
    queryset = Photo.objects.all()
    serializer_class = PhotoSerializer
    permission_classes = (IsAuthenticated,)

    def get_queryset(self):
        if self.kwargs.get('user') == 'me' and self.request.user.is_authenticated():
            self.kwargs['user'] = self.request.user.pk
        queryset = super().get_queryset().accessible_for(self.request.user)
        return queryset.filter(user_id=self.kwargs.get('user'))


class UserFriendsList(ListAPIView):
    serializer_class = UserSerializer
    permission_classes = (IsAuthenticated,)

    def get_queryset(self):
        if self.kwargs.get('user') == 'me' and self.request.user.is_authenticated():
            self.kwargs['user'] = self.request.user.pk
        try:
            user = User.objects.filter(pk=self.kwargs.get('user')).first()
        except:
            return User.objeccts.none()
        return get_friends(user)

    def post(self, request, *args, **kwargs):
        if kwargs.get('user') == 'me' and request.user.is_authenticated():
            kwargs['user'] = request.user.pk
        user = User.objects.get(pk=kwargs['user'])
        social = UserSocialAuth.objects.get(provider='facebook', user=user)
        uid = social.uid
        access_token = social.extra_data['access_token']
        res = requests.get("https://graph.facebook.com/v2.9/" + uid
                           + "/friends?access_token=" + access_token)
        targets = res.json()['data']
        id_list = [target['id'] for target in targets]
        friends = User.objects.filter(
            social_auth__provider='facebook',
            social_auth__uid__in=id_list)

        try:
            profile = user.userprofile
            profile.friends.clear()
        except:
            profile = UserProfile(user=user)
            profile.save()
        profile.friends.add(user)
        profile.friends.add(*friends)

        return Response(None, status=status.HTTP_200_OK)
