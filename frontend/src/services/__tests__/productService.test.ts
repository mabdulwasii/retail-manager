/* eslint-disable @typescript-eslint/no-explicit-any */
import { productService } from '../productService'
import { getMockProduct, getMockProductsList } from '@/testData/products'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'

const server = setupServer(
  // Get products
  http.get('*/shops/:shopId/products', () => {
    return HttpResponse.json(getMockProductsList())
  }),

  // Get single product
  http.get('*/products/:productId', ({ params }) => {
    return HttpResponse.json(getMockProduct({ id: params.productId as string }))
  }),

  // Create product
  http.post('*/shops/:shopId/products', async ({ request }) => {
    const body = await request.json() as any
    return HttpResponse.json(getMockProduct({ id: 'new-prod', name: body.name }))
  }),

  // Update product
  http.patch('*/products/:productId', async ({ params, request }) => {
    const body = await request.json() as any
    return HttpResponse.json(getMockProduct({ id: params.productId as string, ...body }))
  }),

  // Delete product
  http.delete('*/products/:productId', () => {
    return new HttpResponse(null, { status: 204 })
  }),

  // Update product status
  http.patch('*/products/:productId/status', ({ params, request }) => {
    const url = new URL(request.url)
    const status = url.searchParams.get('status')
    return HttpResponse.json(getMockProduct({ id: params.productId as string, status }))
  })
)

describe('productService with MSW', () => {
  beforeAll(() => {
    server.listen({ onUnhandledRequest: 'warn' })
  })

  afterEach(() => {
    server.resetHandlers()
  })

  afterAll(() => {
    server.close()
  })

  describe('getProducts', () => {
    it('should fetch products successfully', async () => {
      const products = await productService.getProducts('shop1')

      expect(products).toBeDefined()
      expect(products.content).toHaveLength(3)
      expect(products.content[0].name).toBe('Laptop Computer')
      expect(products.totalElements).toBe(3)
    })

    it('should fetch products with filters', async () => {
      const params = {
        page: 0,
        size: 10,
        search: 'laptop',
        categoryId: 'cat1',
        status: 'ACTIVE'
      }

      const products = await productService.getProducts('shop1', params)

      expect(products).toBeDefined()
      expect(Array.isArray(products.content)).toBe(true)
    })

    it('should fetch products with price range', async () => {
      const params = {
        minPrice: 20,
        maxPrice: 1000
      }

      const products = await productService.getProducts('shop1', params)

      expect(products).toBeDefined()
    })

    it('should handle fetch error', async () => {
      server.use(
        http.get('*/shops/:shopId/products', () => {
          return HttpResponse.json(
            { message: 'Internal error' },
            { status: 500 }
          )
        })
      )

      await expect(productService.getProducts('shop1')).rejects.toThrow()
    })
  })

  describe('getProduct', () => {
    it('should fetch single product successfully', async () => {
      const product = await productService.getProduct('prod1')

      expect(product).toBeDefined()
      expect(product.id).toBe('prod1')
      expect(product.name).toBe('Laptop Computer')
      expect(product.price).toBe(1200.00)
    })

    it('should handle 404 error', async () => {
      server.use(
        http.get('*/products/:productId', () => {
          return HttpResponse.json(
            { message: 'Product not found' },
            { status: 404 }
          )
        })
      )

      await expect(productService.getProduct('invalid')).rejects.toThrow()
    })
  })

  describe('createProduct', () => {
    it('should create product successfully', async () => {
      const newProduct = await productService.createProduct('shop1', {
        name: 'New Product',
        sku: 'NEW-001',
        price: 99.99,
        cost: 50.00,
        categoryId: 'cat1',
        description: 'Test product'
      })

      expect(newProduct).toBeDefined()
      expect(newProduct.id).toBe('new-prod')
      expect(newProduct.name).toBe('New Product')
    })

    it('should handle validation error', async () => {
      server.use(
        http.post('*/shops/:shopId/products', () => {
          return HttpResponse.json(
            { message: 'SKU already exists' },
            { status: 400 }
          )
        })
      )

      await expect(
        productService.createProduct('shop1', {
          name: 'Duplicate',
          sku: 'DUP-001',
          price: 50,
          cost: 30,
          categoryId: 'cat1'
        })
      ).rejects.toThrow()
    })
  })

  describe('updateProduct', () => {
    it('should update product successfully', async () => {
      const updated = await productService.updateProduct('prod1', {
        name: 'Updated Laptop',
        price: 1300.00
      })

      expect(updated).toBeDefined()
      expect(updated.id).toBe('prod1')
      expect(updated.name).toBe('Updated Laptop')
    })

    it('should handle update error', async () => {
      server.use(
        http.patch('*/products/:productId', () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 403 }
          )
        })
      )

      await expect(
        productService.updateProduct('prod1', { name: 'Test' })
      ).rejects.toThrow()
    })
  })

  describe('deleteProduct', () => {
    it('should delete product successfully', async () => {
      await expect(
        productService.deleteProduct('prod1')
      ).resolves.not.toThrow()
    })

    it('should handle delete error', async () => {
      server.use(
        http.delete('*/products/:productId', () => {
          return HttpResponse.json(
            { message: 'Product has sales history' },
            { status: 400 }
          )
        })
      )

      await expect(productService.deleteProduct('prod1')).rejects.toThrow()
    })
  })

  describe('updateProductStatus', () => {
    it('should update product status successfully', async () => {
      const updated = await productService.updateProductStatus('prod1', 'INACTIVE')

      expect(updated).toBeDefined()
      expect(updated.id).toBe('prod1')
      expect(updated.status).toBe('INACTIVE')
    })

    it('should handle status update error', async () => {
      server.use(
        http.patch('*/products/:productId/status', () => {
          return HttpResponse.json(
            { message: 'Invalid status' },
            { status: 400 }
          )
        })
      )

      await expect(
        productService.updateProductStatus('prod1', 'INVALID')
      ).rejects.toThrow()
    })
  })

  describe('generateSKU', () => {
    it('should generate unique SKU', () => {
      const sku1 = productService.generateSKU()
      const sku2 = productService.generateSKU()

      expect(sku1).toMatch(/^PRD-/)
      expect(sku2).toMatch(/^PRD-/)
      expect(sku1).not.toBe(sku2)
    })
  })

  describe('searchProducts', () => {
    it('should search products successfully', async () => {
      const products = await productService.searchProducts('shop1', 'laptop')

      expect(products).toBeDefined()
      expect(Array.isArray(products)).toBe(true)
    })

    it('should handle search error', async () => {
      server.use(
        http.get('*/shops/:shopId/products', () => {
          return HttpResponse.json(
            { message: 'Search failed' },
            { status: 500 }
          )
        })
      )

      await expect(
        productService.searchProducts('shop1', 'test')
      ).rejects.toThrow()
    })
  })
})
