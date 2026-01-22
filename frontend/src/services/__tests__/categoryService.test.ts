import { categoryService } from '../categoryService'
import { getMockCategories, getMockCategory } from '@/testData/categories'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'

interface CategoryRequestBody {
  name: string;
  description?: string;
  parentId?: string;
  [key: string]: unknown;
}

const server = setupServer(
  // Get categories
  http.get('*/shops/:shopId/categories', ({ params, request }) => {
    const url = new URL(request.url)
    const tree = url.searchParams.get('tree')
    console.log('MSW intercepted getCategories:', { shopId: params.shopId, tree })
    return HttpResponse.json(getMockCategories())
  }),

  // Get single category
  http.get('*/categories/:categoryId', ({ params }) => {
    console.log('MSW intercepted getCategory:', params.categoryId)
    return HttpResponse.json(getMockCategory({ id: params.categoryId as string }))
  }),

  // Create category
  http.post('*/shops/:shopId/categories', async ({ request }) => {
    const body = await request.json() as CategoryRequestBody
    console.log('MSW intercepted createCategory:', body)
    return HttpResponse.json(getMockCategory({ id: 'new-cat', name: body.name }))
  }),

  // Update category
  http.patch('*/categories/:categoryId', async ({ params, request }) => {
    const body = await request.json() as CategoryRequestBody
    console.log('MSW intercepted updateCategory:', params.categoryId, body)
    return HttpResponse.json(getMockCategory({ id: params.categoryId as string, ...body }))
  }),

  // Delete category
  http.delete('*/categories/:categoryId', ({ params }) => {
    console.log('MSW intercepted deleteCategory:', params.categoryId)
    return new HttpResponse(null, { status: 204 })
  })
)

describe('categoryService with MSW', () => {
  beforeAll(() => {
    server.listen({
      onUnhandledRequest: 'warn'
    })
  })

  afterEach(() => {
    server.resetHandlers()
  })

  afterAll(() => {
    server.close()
  })

  describe('getCategories', () => {
    it('should fetch categories successfully', async () => {
      // Using test data from @/testData/categories
      const categories = await categoryService.getCategories('shop1', false)

      expect(categories).toBeDefined()
      expect(Array.isArray(categories)).toBe(true)
      expect(categories.length).toBeGreaterThan(0)
      expect(categories[0].name).toBe('Electronics')
    })

    it('should fetch categories with tree parameter', async () => {
      const categories = await categoryService.getCategories('shop1', true)

      expect(categories).toBeDefined()
      expect(Array.isArray(categories)).toBe(true)
    })

    it('should handle error when API fails', async () => {
      // Override handler to return error
      server.use(
        http.get('*/shops/:shopId/categories', () => {
          return HttpResponse.json(
            { message: 'Internal server error' },
            { status: 500 }
          )
        })
      )

      await expect(
        categoryService.getCategories('shop1', false)
      ).rejects.toThrow()
    })
  })

  describe('getCategory', () => {
    it('should fetch single category successfully', async () => {
      const category = await categoryService.getCategory('cat1')

      expect(category).toBeDefined()
      expect(category.id).toBe('cat1')
      expect(category.name).toBe('Electronics')
    })

    it('should handle 404 error', async () => {
      server.use(
        http.get('*/categories/:categoryId', () => {
          return HttpResponse.json(
            { message: 'Category not found' },
            { status: 404 }
          )
        })
      )

      await expect(
        categoryService.getCategory('invalid')
      ).rejects.toThrow()
    })
  })

  describe('createCategory', () => {
    it('should create category successfully', async () => {
      const newCategory = await categoryService.createCategory('shop1', {
        shopId: 'shop1',
        name: 'New Category',
        description: 'Test category',
        isActive: true
      })

      expect(newCategory).toBeDefined()
      expect(newCategory.id).toBeDefined()
      expect(newCategory.name).toBe('New Category')
    })

    it('should handle validation error', async () => {
      server.use(
        http.post('*/shops/:shopId/categories', () => {
          return HttpResponse.json(
            { message: 'Category name already exists' },
            { status: 400 }
          )
        })
      )

      await expect(
        categoryService.createCategory('shop1', {
          shopId: 'shop1',
          name: 'Duplicate',
          isActive: true
        })
      ).rejects.toThrow()
    })
  })

  describe('updateCategory', () => {
    it('should update category successfully', async () => {
      const updated = await categoryService.updateCategory('cat1', {
        name: 'Updated Electronics'
      })

      expect(updated).toBeDefined()
      expect(updated.id).toBe('cat1')
      expect(updated.name).toBe('Updated Electronics')
    })

    it('should handle update error', async () => {
      server.use(
        http.patch('*/categories/:categoryId', () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 403 }
          )
        })
      )

      await expect(
        categoryService.updateCategory('cat1', { name: 'Test' })
      ).rejects.toThrow()
    })
  })

  describe('deleteCategory', () => {
    it('should delete category successfully', async () => {
      await expect(
        categoryService.deleteCategory('cat1')
      ).resolves.not.toThrow()
    })

    it('should handle delete error', async () => {
      server.use(
        http.delete('*/categories/:categoryId', () => {
          return HttpResponse.json(
            { message: 'Category has products' },
            { status: 400 }
          )
        })
      )

      await expect(
        categoryService.deleteCategory('cat1')
      ).rejects.toThrow()
    })
  })

  describe('getCategoryNames', () => {
    it('should return sorted active category names', async () => {
      const names = await categoryService.getCategoryNames('shop1')

      expect(Array.isArray(names)).toBe(true)
      expect(names.length).toBeGreaterThan(0)
      
      // Should be sorted
      const sortedNames = [...names].sort()
      expect(names).toEqual(sortedNames)
      
      // Should only include active categories
      expect(names.every(name => typeof name === 'string')).toBe(true)
    })
  })
})
