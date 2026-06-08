using Hanger.Application.Configuration;
using Hanger.Infrastructure.Configuration;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Mvc;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddProblemDetails();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);

var app = builder.Build();

app.UseSwagger();
app.UseSwaggerUI(options =>
{
    options.SwaggerEndpoint("/swagger/v1/swagger.json", "Hanger API v1");
    options.RoutePrefix = "swagger";
});

app.UseExceptionHandler(exceptionApp =>
{
    exceptionApp.Run(async context =>
    {
        var exception = context.Features.Get<IExceptionHandlerFeature>()?.Error;

        var problem = new ProblemDetails
        {
            Title = "Erro interno do servidor.",
            Detail = app.Environment.IsDevelopment() ? exception?.Message : "Ocorreu um erro ao processar a requisicao.",
            Status = StatusCodes.Status500InternalServerError,
            Instance = context.Request.Path
        };

        context.Response.StatusCode = StatusCodes.Status500InternalServerError;
        await context.Response.WriteAsJsonAsync(problem);
    });
});

app.UseStatusCodePages(async context =>
{
    var response = context.HttpContext.Response;
    if (response.HasStarted || response.StatusCode < 400)
    {
        return;
    }

    var problem = new ProblemDetails
    {
        Title = response.StatusCode switch
        {
            StatusCodes.Status404NotFound => "Recurso nao encontrado.",
            StatusCodes.Status405MethodNotAllowed => "Metodo HTTP nao permitido.",
            _ => "Erro HTTP."
        },
        Status = response.StatusCode,
        Instance = context.HttpContext.Request.Path
    };

    await response.WriteAsJsonAsync(problem);
});

app.MapGet("/", () => Results.Ok(new
{
    name = "Hanger API",
    swagger = "/swagger",
    routes = new[]
    {
        "GET /api/{table}",
        "GET /api/{table}/{id}",
        "GET /api/{table}/by-key?key=value",
        "POST /api/{table}",
        "PUT /api/{table}/{id}",
        "PUT /api/{table}/by-key?key=value",
        "DELETE /api/{table}/{id}",
        "DELETE /api/{table}/by-key?key=value"
    }
}));

app.MapControllers();

app.Run();
