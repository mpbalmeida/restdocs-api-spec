package dev.marcosalmeida.restdocs.apispec.sample;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.data.rest.webmvc.RestMediaTypes.TEXT_URI_LIST_VALUE;

@RepositoryRestController
@RequestMapping("/carts")
public class CartController {

    private final CartRepository cartRepository;

    private final ProductRepository productRepository;

    private final EntityLinks entityLinks;

    private final CartResourceResourceAssembler cartResourceResourceAssembler;

    public CartController(CartRepository cartRepository, ProductRepository productRepository, EntityLinks entityLinks, CartResourceResourceAssembler cartResourceResourceAssembler) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.entityLinks = entityLinks;
        this.cartResourceResourceAssembler = cartResourceResourceAssembler;
    }

    @PostMapping
    public ResponseEntity<CartResourceResourceAssembler.CartResource> create() {
        Cart cart = cartRepository.save(new Cart());
        return ResponseEntity.created(entityLinks.linkForSingleResource(cart).toUri())
                .body(cartResourceResourceAssembler.toResource(cart));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResourceResourceAssembler.CartResource> get(@PathVariable Long cartId) {
        return cartRepository.findById(cartId)
                .map(cartResourceResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{cartId}/order")
    public ResponseEntity<CartResourceResourceAssembler.CartResource> order(@PathVariable Long cartId) {
        return cartRepository.findById(cartId)
                .map(cart -> {
                    cart.setOrdered(true);
                    return cartRepository.save(cart);
                })
                .map(cartResourceResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/{cartId}/products", consumes = TEXT_URI_LIST_VALUE)
    public ResponseEntity<CartResourceResourceAssembler.CartResource> addProducts(@PathVariable Long cartId, @RequestBody Resources<Object> resource) {
        return cartRepository.findById(cartId)
                .map(cart -> {
                    resource.getLinks().stream()
                            .map(link -> link.getHref().substring(link.getHref().lastIndexOf("/") + 1))
                            .map(Long::valueOf)
                            .map(productRepository::findById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(product -> cart.getProducts().add(product));
                    return cartRepository.save(cart);
                })
                .map(cartResourceResourceAssembler::toResource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
