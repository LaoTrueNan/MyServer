//Joseph
#include <CirLinkedList.h>
template <class T>
void Joseph(CirLinkedList<T>& cirlist,int n,int m){
    CirLinkNode<T> *node = cirlist.Locate(1), *pre = NULL;
    //pre就是要出列的前一个结点
    for(int i = 0;i<n-1;i++){
        for(int j = 1;j<m;j++){
            pre = node;
            node = node->next;
        }
        cout<<"out: "<<node->data<<endl;
        pre->next = node->next;
        delete node;
        node = pre->next;
    }
    cout<<"winner is :"<<node->data<<endl;
};
int main(){
    CirLinkedList<int> list;
    int i,n,m;
    cin>>n>>m;
    for(i=1;i<=n;i++){
        list.RearInsert(i);
    }
    Joseph(list,n,m);
    return 0;
}