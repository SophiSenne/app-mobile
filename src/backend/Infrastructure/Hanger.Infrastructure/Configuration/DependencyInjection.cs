using Hanger.Application.Abstractions;
using Hanger.Infrastructure.Data;
using Hanger.Infrastructure.Repositories;
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
        services.AddScoped<ITableCrudRepository, PostgresTableCrudRepository>();
        services.AddScoped<IHangerRepository, HangerRepository>();

        return services;
    }
}
