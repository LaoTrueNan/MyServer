#ifndef STACK_H
#define STACK_H
#include <LinkNode.h>
#include <iostream>
using namespace std;

template <class T>
class Stack{
    private:
    LinkNode<T> *top;
    public:
    Stack(){
        top = NULL;
    }
    ~Stack(){makeEmpty();}
    void makeEmpty();
    bool isEmpty(){return (top == NULL) ? true:false;}
    void push(const T& d);
    bool pop(T& d);
    bool getTop(T& x) const;
    int getSize()const;
};
template <class T>
void Stack<T>::makeEmpty(){
    LinkNode<T> *current;
    while(top!=NULL){
        current = top;
        top = top->next;
        delete current;
    }
};
template <class T>
void Stack<T>::push(const T&d){
    top = new LinkNode<T>(d,top);
}

template <class T>
bool Stack<T>::pop(T &d){
    if(!isEmpty()){
        LinkNode<T> *p = top;
        top = top->next;
        d = p->data;
        delete p;
        return true;
    }
    return false;
}
template <class T>
int Stack<T>::getSize()const{
    LinkNode<T> *p = top;
    int cnt =0;
    while(p!=NULL){
        p = p->next;
        cnt++;
    }
    return cnt;
}

template <class T>
bool Stack<T>::getTop(T& x)const{
    if(isEmpty()){
        return false;
    }else{x=top->data;
    return true;}
}
template <class T>
ostream& operator << (ostream& out,Stack<T>& s){
    LinkNode<T> *current = s.top;
    while(current!=NULL){
        out<<current->data<<endl;
        current = current->next;
    }
    return out;
};
#endif
