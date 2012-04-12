#include<iostream>
using namespace std;
int a[100];
void Mergesort(int,int);
void Merge(int,int,int);
int main()
{
    int i,n;
    cout<<"\nEnter the no. of elements::";
      cin>>n;
    for(i=0;i<n;i++)
      cin>>a[i];
    Mergesort(0,n-1);
    cout<<"\n\nSorted Array::\n";
    for(i=0;i<n;i++)
     cout<<a[i]<<' ';
}
void Mergesort(int beg,int end)
{
   int mid = (beg + end)/2;
   if(beg<end)
   {
     Mergesort(beg,mid);
     Mergesort(mid+1,end);
   }
   Merge(beg,mid,end);
}
void Merge(int beg,int mid, int end)
{
   int *p = new int [end-beg+1];
   int a1=beg,a2=mid+1,count=0,i;
   while(a1<=mid && a2<=end)
   {
     if(a[a1]<=a[a2])
      p[count++]=a[a1++];
     else
      p[count++]=a[a2++];
   }
   if(a1>mid)
    while(a2<=end)
     p[count++]=a[a2++];
   if(a2>end)
    while(a1<=mid)
     p[count++]=a[a1++];
   for(i=0;i<count;i++)
    a[beg+i] = p[i];
   delete []p;
}
