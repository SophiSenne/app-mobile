using Hanger.Application.Services;
using Microsoft.Extensions.DependencyInjection;
using Hanger.Application.Abstractions;

namespace Hanger.Application.Configuration;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<IUsersService, UsersService>();
        services.AddScopde<IAuthService, AuthService>();
        return services;
    }
}
