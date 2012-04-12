#include <fstream>
#include <string>
#include <stdlib.h>
#include <iostream>
using namespace std;

bool Check_Err(string err)
{
    ifstream is;
    is.open (err.c_str());
    is.seekg (0, ios::end);
    int l = is.tellg();
    is.close();
    if(l < 5)
        return false;
    else
        return true;
}

int main(int argc, char *argv[])
{
    string command, file, errfile, obfile;
    file.assign(argv[1]);
    int ls = file.find_last_of('/');
    errfile.assign(file.substr(0,ls+1));
    errfile.append("err");
    ls = file.find_last_of('.');
    obfile.assign(file.substr(0,ls+1));
    obfile.append("o");
    
    remove(errfile.c_str());
    remove(obfile.c_str());    
    remove(file.substr(0,file.find_last_of('.')).c_str());
    
    command.assign("g++ -c ");
    command.append(file);
    command.append(" -o ");
    command.append(obfile);
    command.append(" > ");
    command.append(errfile);
    command.append(" 2>&1");
    system(command.c_str());
    
    if(!Check_Err(errfile))
    {
        command.assign("g++ -o ");
        ls = file.find_last_of('.');
        command.append(file.substr(0,ls));
        command.append(" ");
        command.append(obfile);
        command.append(" > ");
        command.append(errfile);
        command.append(" 2>&1 -lm");
        system(command.c_str());
        remove(obfile.c_str());
        if(!Check_Err(errfile))
        {
            remove(errfile.c_str());
        }
    }
}
