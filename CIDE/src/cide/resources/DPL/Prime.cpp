#include<stdio.h>
#include<math.h>
int Chk_Prime(int n)
{
  int i,flag = 1;
  for(i=2;i<=sqrt(n);(i>2)?i+=2:i++)
    if(Chk_Prime(i))
      if(n%i == 0)
       {
         flag = 0;
         break;
       }
  return flag;
}
int main()
{
  int n;
  scanf("%d",&n);
  if(Chk_Prime(n))
   printf("\nPrime");
  else
   printf("\nNot Prime");
}
