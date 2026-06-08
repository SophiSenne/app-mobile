using Hanger.Application.Services;
using Microsoft.Extensions.DependencyInjection;

namespace Hanger.Application.Configuration;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddScoped<ITableCrudService, TableCrudService>();
        services.AddScoped<IHangerService, HangerService>();
        return services;
    }
}
