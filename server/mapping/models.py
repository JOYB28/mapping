from django.db import models
from django.db.models import Q
from django.contrib.auth.models import User


def get_friends(user):
    if user.is_authenticated():
        try:
            profile = user.userprofile
        except:
            profile = UserProfile(user=user)
            profile.save()
            profile.friends.add(user)
        return profile.friends.all()
    return User.objects.none()


class UserProfile(models.Model):
    user = models.OneToOneField(User, null=False)
    friends = models.ManyToManyField(User, related_name='friends')

    def __str__(self):
        return self.user.username


def user_directory_path(instance, filename):
    return 'user_{user_id}/{filename}'.format(user_id=instance.user_id, filename=filename)


class PhotoQuerySet(models.QuerySet):
    def accessible_for(self, user):
        q = Q(is_public=True)
        q |= Q(user__in=get_friends(user))
        return self.filter(q).distinct()

    def filter_friends(self, user):
        return self.filter(user__in=get_friends(user))


class PhotoManager(models.Manager):
    def get_queryset(self):
        return PhotoQuerySet(self.model, using=self._db)

    def accessible_for(self, user):
        return self.get_queryset().accessible_for(user)

    def filter_friends(self, user):
        return self.get_queryset().filter_friends(user)


class Photo(models.Model):
    user = models.ForeignKey(User, null=False)
    image = models.ImageField(upload_to=user_directory_path, null=False)
    desc = models.TextField()
    pub_date = models.DateTimeField(auto_now_add=True)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    grid = models.IntegerField()
    is_public = models.BooleanField()

    objects = PhotoManager()

    def __str__(self):
        return self.desc
