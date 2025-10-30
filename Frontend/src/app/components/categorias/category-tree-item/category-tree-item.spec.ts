import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryTreeItem } from './category-tree-item';

describe('CategoryTreeItem', () => {
  let component: CategoryTreeItem;
  let fixture: ComponentFixture<CategoryTreeItem>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryTreeItem]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoryTreeItem);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
