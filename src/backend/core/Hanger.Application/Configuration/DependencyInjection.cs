using Hanger.Application.Services;
using Microsoft.Extensions.DependencyInjection;
using Hanger.Application.Abstractions;

namespace Hanger.Application.Configuration;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IUsersService, UsersService>();
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IPostsService, PostsService>();
        services.AddScoped<IFollowsService, FollowsService>();
        services.AddScoped<ILikesService, LikesService>();
        services.AddScoped<ICommentsService, CommentsService>();
        services.AddScoped<INotificationsService, NotificationsService>();
        services.AddScoped<ICategoriesService, CategoriesService>();
        services.AddScoped<IImageService, ImageService>();

        return services;
    }
}
