#include<iostream>
#include<stdlib.h>
using namespace std;
int comp (const void *a, const void *b)
     {
       const int *da = (const int *) a;
       const int *db = (const int *) b;
     
       return (*da > *db) - (*da < *db);
     }

          {
            int n;
             cin>>n;
            int * a = new int [n];
            for (int i=0;i<n;i++)
               cin>>a[i];
            qsort (a, n, sizeof (int), comp);
            for (int i=0;i<n;i++)
               cout<<" "<<a[i];
          }
