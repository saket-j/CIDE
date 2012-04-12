#include<iostream>
using namespace std;
int a[50];
void Qsort(int,int);
int main()
{
    int n;
    cin>>n;
    int i;
    for(i=0;i<n;i++)
     cin>>a[i];
    Qsort(0,n-1);
    cout<<"\nSorted array::\n";
    for(i=0;i<n;i++)
      cout<<a[i]<<' ';
}
void Qsort(int low,int high)
{
     int temp,i,j;
     if(low<high)
     {
        i=low+1;
        j=high;
        while(i<j)
        {
           while(a[i]<a[low] && i<j)
             i++;
           while(a[j]>a[low] && i<j)
             j--;
           if(i<j)
           {
             temp = a[i];
             a[i] = a[j];
             a[j] = temp;
           }
        }
        if(a[j]>a[low])
         j--;
        temp = a[low];
        a[low] = a[j];
        a[j] = temp;
        Qsort(low,j-1);
        Qsort(j+1,high);
     }
}
