#ifndef LINKNODE_H
#define LINKNODE_H
#include <iostream>
using namespace std;
template <class T>
struct LinkNode{
    T data;
    LinkNode<T> *next;
    LinkNode(LinkNode<T> *n=NULL){
        next = n;
    }
    LinkNode(T d,LinkNode<T> *n=NULL){
        data = d;
        next = n;
    }
    
};
template <class T>
ostream& operator << (ostream& out,LinkNode<T>& node){
    out<<node.data;
    return out;
}
#endif