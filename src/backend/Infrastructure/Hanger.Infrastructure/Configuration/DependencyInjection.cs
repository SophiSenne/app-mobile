using Hanger.Application.Abstractions;
using Hanger.Infrastructure.Data;
using Hanger.Infrastructure.Repositories;
using Hanger.Application.Services;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Npgsql;

namespace Hanger.Infrastructure.Configuration;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("DefaultConnection");
        if (string.IsNullOrWhiteSpace(connectionString))
        {
            throw new InvalidOperationException("Configure ConnectionStrings:DefaultConnection no appsettings ou nas variaveis de ambiente.");
        }

        services.AddSingleton(new HangerSchema());
        services.AddSingleton(_ => NpgsqlDataSource.Create(connectionString));
        services.AddScoped<IUsersRepository, UsersRepository>();
        services.AddScoped<IAuthRepository, AuthRepository>();
        services.AddScoped<IPostsRepository, PostsRepository>();
        services.AddScoped<IFollowsRepository, FollowsRepository>();
        services.AddScoped<ILikesRepository, LikesRepository>();
        services.AddScoped<ICommentsRepository, CommentsRepository>();
        services.AddScoped<INotificationsRepository, NotificationsRepository>();
        services.AddScoped<ICategoriesRepository, CategoriesRepository>();

        return services;
    }
}