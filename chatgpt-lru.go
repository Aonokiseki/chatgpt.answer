package main

import "container/list"

type LRUCache struct {
	maxSize int
	cache   map[string]*list.Element
	list    *list.List
}

type entry struct {
	key   string
	value interface{}
}

func NewLRUCache(maxSize int) *LRUCache {
	return &LRUCache{
		maxSize: maxSize,
		cache:   make(map[string]*list.Element),
		list:    list.New(),
	}
}

func (c *LRUCache) Get(key string) (interface{}, bool) {
	if elem, ok := c.cache[key]; ok {
		c.list.MoveToFront(elem)
		return elem.Value.(*entry).value, true
	}
	return nil, false
}

func (c *LRUCache) Put(key string, value interface{}) {
	if elem, ok := c.cache[key]; ok {
		c.list.MoveToFront(elem)
		elem.Value.(*entry).value = value
		return
	}
	if c.list.Len() >= c.maxSize {
		elem := c.list.Back()
		delete(c.cache, elem.Value.(*entry).key)
		c.list.Remove(elem)
	}
	elem := c.list.PushFront(&entry{key, value})
	c.cache[key] = elem
}

func main() {
	cache := NewLRUCache(3)

	cache.Put("key1", 1)
	cache.Put("key2", 2)
	cache.Put("key3", 3)

	if value, ok := cache.Get("key1"); ok {
		println(value.(int)) // should print 1
	}

	cache.Put("key4", 4)

	if _, ok := cache.Get("key2"); !ok {
		println("key2 not found") // should print "key2 not found"
	}
}
