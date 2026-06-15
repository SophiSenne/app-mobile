namespace Hanger.Application.Services;
using Domain.Interfaces;

public class UsersService : IUsersService {
    private readonly IUsersRepository _usersRepository;
    public UsersService (IUsersRepository usersRepository){
        _usersRepository = usersRepository;
    }

}