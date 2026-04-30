import { Component, signal } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-public-help',
  standalone: true,
  imports: [NgFor, NgIf, RouterLink],
  templateUrl: './help.html',
})
export class PublicHelpComponent {
  readonly faqs = signal([
    {
      question: 'How does role-based login work?',
      answer: 'After authentication, the application reads the role from the JWT and redirects you to the matching portal automatically.',
      open: true,
    },
    {
      question: 'Can customers upload pet photos?',
      answer: 'Yes. Customers can now attach pet images directly while creating or updating pet profiles.',
      open: false,
    },
    {
      question: 'Can anyone register as an admin?',
      answer: 'No. Only customer and veterinarian registration is exposed in the public interface. Admin access is managed internally.',
      open: false,
    },
    {
      question: 'What can veterinarians update?',
      answer: 'Veterinarians can manage profile details, clinic information, and their available working slots from their own portal.',
      open: false,
    },
  ]);

  toggle(index: number) {
    this.faqs.update((items) =>
      items.map((item, currentIndex) =>
        currentIndex === index ? { ...item, open: !item.open } : item,
      ),
    );
  }
}
