#include<iostream>
using namespace std;
struct bst
{
  int val;
  bst *left,*right;
};
void Inorder(bst * root)
{
  if(root != NULL)
  {
  bst * temp = root;
  Inorder(temp->left);
  cout<<temp->val<<' ';
  Inorder(temp->right);
  }
}
bst * Insert(bst * root,bst * temp)
{
  if(root == NULL)
    root = temp;
  else if(root->val > temp->val)
   root->left=Insert(root->left,temp);
  else
   root->right=Insert(root->right,temp);
  return root;
}
int main()
{
  int n,i;
  bst *root = NULL,*temp;
  cin>>n;
  for(i=0;i<n;i++)
  {
    temp = new bst;
    cin>>temp->val;
    temp->left = temp->right = NULL;
    root = Insert(root,temp);
  }
  Inorder(root);
}
  
