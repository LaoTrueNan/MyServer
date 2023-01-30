#ifndef CIRLINKEDLIST_H
#define CIRLINKEDLIST_H
#include <iostream>
using namespace std;

template <class T>
struct CirLinkNode{
    T data;
    CirLinkNode<T> *next;
    CirLinkNode(CirLinkNode<T> *n = NULL){
        next = n;
    }
    CirLinkNode(T d,CirLinkNode<T> *n=NULL){
        data = d;
        next = n;
    }
};

template <class T>
class CirLinkedList{
private:
    CirLinkNode<T> *first,*last;
public:
    CirLinkedList(){
        first = new CirLinkNode<T>();
        last = first;
    }
    CirLinkedList(const T& x){
        first = new CirLinkNode<T>(x);
        last = first;
    }
    int Length()const;
    bool IsEmpty(){
        return first == last?true:false;
    }
    CirLinkNode<T> *getHead()const {
        return first;
    };
    CirLinkNode<T> *Locate(int i);
    bool RearInsert(const T& x);//最末端插入元素
};

template <class T>
CirLinkNode<T>* CirLinkedList<T>::Locate(int i){
    CirLinkNode<T> *current = first;
    int k=0;
    while(current != NULL && k<i){
        current = current->next;
        k++;
    }
    return current;
}

template <class T>
int CirLinkedList<T>::Length() const{
    CirLinkNode<T> *current = first->next;
    int cnt = 0;
    while(current != NULL){
        cnt++;
        current = current->next;
    }
    return cnt;
}

template <class T>
bool CirLinkedList<T>::RearInsert(const T& x){
    CirLinkNode<T> *current = last;
    CirLinkNode<T> *newNode = new CirLinkNode<T>(x);
    last->next = newNode;
    last = newNode;
    newNode->next = first->next;
    //newNode->next = first;
    //按照上面这样写的话会使得头结点也被计算在内，比如约瑟夫问题
    return true;
}
#endif