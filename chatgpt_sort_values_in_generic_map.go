package main

import (
	"fmt"
	"sort"
)

type KeyValuePair struct {
	Key   interface{}
	Value interface{}
}

type PairList []KeyValuePair

func (p PairList) Swap(i, j int)      { p[i], p[j] = p[j], p[i] }
func (p PairList) Len() int           { return len(p) }
func (p PairList) Less(i, j int) bool { return fmt.Sprintf("%v", p[i].Value) < fmt.Sprintf("%v", p[j].Value) }

func main() {
	m := map[string]interface{}{
		"key1": 5,
		"key2": 2,
		"key3": 10,
		"key4": 3,
		"key5": 8,
	}

	pairs := make(PairList, len(m))
	i := 0
	for k, v := range m {
		pairs[i] = KeyValuePair{k, v}
		i++
	}

	sort.Sort(pairs)

	for _, pair := range pairs {
		fmt.Println(pair.Key, pair.Value)
	}
}
