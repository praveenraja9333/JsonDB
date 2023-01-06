package org.vrp.utils.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonKeys {
    private Node<Character> root;
    private Node<Character> tempstate;
    private final ArrayList<String> populatelist = new ArrayList<>();

    class Node<V> {
        V value;
        protected List<Node<V>> children;

        Node(V value) {
            this.value = value;
            children = new ArrayList<>();
        }
    }

    public void add(String KeyString) {
        char[] _carray = KeyString.toCharArray();
        tempstate = root;
        if (root == null) {
            root = new Node<Character>(_carray[0]);
            tempstate = root;
        }
        _carray = Arrays.copyOfRange(_carray, 1, _carray.length);
        for (char i : _carray) {
            addCharacter(i, tempstate);
        }
        tempstate = null;
    }

    private void addCharacter(Character value, Node<Character> node) {

        boolean _insertflag = false;
        for (Node child : node.children) {
            if (child.value == value) {
                _insertflag = true;
                tempstate = child;
            }
        }
        if (!_insertflag) {
            Node<Character> childnode = new Node<>(value);
            node.children.add(childnode);
            tempstate = childnode;
        }
    }

    private void getdfs(Node<Character> node, String fixed, int pos, String create, boolean regflag) {
        boolean end = false;
        Character currentchar = null;
        String currentnodevalue = node.value.toString();
        if (pos >= fixed.length()) {
            end = true;
        } else {
            currentchar = fixed.charAt(pos);
        }
        if (!end && fixed.charAt(pos) == '\\' && fixed.charAt(pos + 1) == '*') {
            pos = pos + 2;
            getdfs(node, fixed, pos, create, true);
            return;
        }
        if (node.value.equals(currentchar)) {
            pos++;
            for (Node<Character> child : node.children) {
                getdfs(child, fixed, pos, create + currentnodevalue, false);
            }
        } else if (end) {
            for (Node<Character> child : node.children) {
                getdfs(child, fixed, pos, create + currentnodevalue, regflag);
            }
        } else if (regflag) {
            for (Node<Character> child : node.children) {
                getdfs(child, fixed, pos, create + currentnodevalue, regflag);
            }
        }
        if(node.children.size()==0 && pos == fixed.length()){
            populatelist.add(create + currentnodevalue);
        }
    }

    public ArrayList<String> get(String key) {
        populatelist.clear();
        boolean regflag = false;
        getdfs(root, key, 0, "", regflag);
        return populatelist;
    }

    public static void main(String[] args) throws FileNotFoundException {
        JsonKeys j = new JsonKeys();
        BufferedInputStream bf = new BufferedInputStream(new FileInputStream(""));
        j.add("abcdfg");
        j.add("abcek");
    }
}
